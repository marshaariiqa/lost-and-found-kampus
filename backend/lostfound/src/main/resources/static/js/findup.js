/* FindUp! - interaksi UI. Logika bisnis tetap di backend; di sini hanya
   validasi awal, loading state, wizard, upload preview, dan toast. */
(function () {
  'use strict';

  var ICON_CHECK = '<svg class="ic" aria-hidden="true"><use href="#i-check"></use></svg>';
  var ICON_X = '<svg class="ic" aria-hidden="true"><use href="#i-x"></use></svg>';

  /* ---------- Toast (komponen tunggal) ---------- */
  function root() {
    var el = document.getElementById('toast-root');
    if (!el) {
      el = document.createElement('div');
      el.id = 'toast-root';
      el.setAttribute('aria-live', 'polite');
      document.body.appendChild(el);
    }
    return el;
  }

  function toast(message, type) {
    if (!message) { return; }
    var t = document.createElement('div');
    t.className = 'toast' + (type === 'error' ? ' error' : '');
    t.setAttribute('role', type === 'error' ? 'alert' : 'status');
    var icon = document.createElement('span');
    icon.className = 'tk';
    icon.innerHTML = type === 'error' ? ICON_X : ICON_CHECK;
    var text = document.createElement('span');
    text.textContent = message;
    t.appendChild(icon);
    t.appendChild(text);
    root().appendChild(t);
    setTimeout(function () {
      t.classList.add('out');
      setTimeout(function () { if (t.parentNode) { t.parentNode.removeChild(t); } }, 300);
    }, type === 'error' ? 5000 : 3200);
  }

  window.FindUp = { toast: toast };

  function showFlash() {
    var f = document.getElementById('flash');
    if (!f) { return; }
    if (f.dataset.sukses) { toast(f.dataset.sukses, 'success'); }
    if (f.dataset.error) { toast(f.dataset.error, 'error'); }
  }

  /* ---------- Util ---------- */
  function $(sel, ctx) { return (ctx || document).querySelector(sel); }
  function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }

  function fieldOk(el) {
    if (el.disabled || el.type === 'hidden') { return true; }
    if (el.type === 'checkbox') { return !el.required || el.checked; }
    if (el.type === 'file') { return !el.required || (el.files && el.files.length > 0); }
    if (el.required && !String(el.value).trim()) { return false; }
    if (el.minLength > 0 && el.value && el.value.length < el.minLength) { return false; }
    if (el.type === 'email' && el.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(el.value)) { return false; }
    return true;
  }

  function mark(el, bad) {
    el.classList.toggle('err', bad);
    var drop = el.closest ? el.closest('.drop') : null;
    if (drop) { drop.classList.toggle('err', bad); }
    // Checkbox persetujuan: sorot barisnya dan tampilkan pesan khususnya
    if (el.type === 'checkbox') {
      var line = el.closest('.checkline');
      if (line) { line.classList.toggle('err', bad); }
      if (el.form && el.name) {
        var msg = el.form.querySelector('[data-err-for="' + el.name + '"]');
        if (msg) { msg.hidden = !bad; }
      }
    }
    el.setAttribute('aria-invalid', bad ? 'true' : 'false');
  }

  /* ---------- Validasi form & loading state ---------- */
  function initForms() {
    $$('form[data-validate]').forEach(function (form) {
      form.setAttribute('novalidate', 'novalidate');

      form.addEventListener('focusout', function (e) {
        var t = e.target;
        // checkbox & file tidak ditandai saat sekadar ditinggalkan; hanya saat mencoba kirim
        if (t.matches && t.matches('[required]') && t.type !== 'file' && t.type !== 'checkbox') { mark(t, !fieldOk(t)); }
      });
      form.addEventListener('input', function (e) {
        var t = e.target;
        if (t.classList && t.classList.contains('err') && fieldOk(t)) { mark(t, false); }
      });
      form.addEventListener('change', function (e) {
        var t = e.target;
        if (t.tagName === 'SELECT') { t.classList.toggle('is-blank', !t.value); }
        if (t.classList && t.classList.contains('err') && fieldOk(t)) { mark(t, false); }
      });

      form.addEventListener('submit', function (e) {
        if (form.dataset.submitting === '1') { e.preventDefault(); return; }
        var first = null;
        $$('input, select, textarea', form).forEach(function (el) {
          var ok = fieldOk(el);
          if (el.matches('[required], [minlength]') || el.type === 'email') { mark(el, !ok); }
          if (!ok && !first) { first = el; }
        });
        var pw1 = $('[data-match-source]', form), pw2 = $('[data-match-target]', form);
        if (pw1 && pw2 && pw1.value !== pw2.value) {
          mark(pw2, true);
          if (!first) { first = pw2; }
        }
        if (first) {
          e.preventDefault();
          if (first.type === 'file') { first.closest('.drop').scrollIntoView({ block: 'center' }); }
          else { first.focus(); if (first.type === 'checkbox') { first.scrollIntoView({ block: 'center' }); } }
          return;
        }
        form.dataset.submitting = '1';
        var btn = e.submitter || $('button[type=submit]', form);
        if (btn) {
          btn.classList.add('loading');
          if (btn.dataset.loading) { btn.dataset.label = btn.textContent; btn.textContent = btn.dataset.loading; }
        }
      });
    });

    // Tombol Kembali (bfcache) tidak boleh macet di status loading
    window.addEventListener('pageshow', function () {
      $$('form[data-submitting]').forEach(function (f) { delete f.dataset.submitting; });
      $$('.btn.loading').forEach(function (b) {
        b.classList.remove('loading');
        if (b.dataset.label) { b.textContent = b.dataset.label; }
      });
    });
  }

  /* ---------- Wizard lapor barang ---------- */
  function initWizard() {
    var wiz = $('[data-wizard]');
    if (!wiz) { return; }
    var steps = $$('[data-step]', wiz);
    var form = wiz.tagName === 'FORM' ? wiz : wiz.closest('form');
    var cur = parseInt(wiz.dataset.start || '1', 10);
    var stp = $('.stp');

    function stepValid(n) {
      var sec = steps[n - 1];
      return $$('[required]', sec).filter(function (el) { return !el.closest('[hidden]'); })
        .every(fieldOk);
    }
    function summary() {
      $$('[data-summary]', wiz).forEach(function (out) {
        var src = form.elements[out.dataset.summary];
        var v = '';
        if (src) {
          v = src.tagName === 'SELECT' ? (src.options[src.selectedIndex] || {}).text || '' : src.value;
        }
        out.textContent = v && v.trim() ? v : '-';
      });
      var jenis = form.elements['jenis'] ? form.elements['jenis'].value : 'hilang';
      var j = $('[data-summary-jenis]', wiz);
      if (j) { j.textContent = jenis === 'temuan' ? 'Barang Temuan' : 'Barang Hilang'; }
    }
    function render() {
      steps.forEach(function (s, i) { s.hidden = (i + 1) !== cur; });
      if (stp) {
        $$('.st', stp).forEach(function (st, i) {
          st.classList.toggle('on', i + 1 === cur);
          st.classList.toggle('done', i + 1 < cur);
        });
        $$('.ln', stp).forEach(function (ln, i) { ln.classList.toggle('on', i + 1 < cur); });
      }
      if (cur === steps.length) { summary(); }
      sync();
    }
    function sync() {
      $$('[data-next]', wiz).forEach(function (b) {
        var ok = stepValid(cur);
        b.disabled = !ok;
        var h = b.parentElement.querySelector('[data-gh]');
        if (h) { h.style.visibility = ok ? 'hidden' : 'visible'; }
      });
    }
    wiz.addEventListener('click', function (e) {
      var n = e.target.closest('[data-next]'), p = e.target.closest('[data-prev]');
      if (n && stepValid(cur) && cur < steps.length) { cur++; render(); window.scrollTo({ top: 0, behavior: 'smooth' }); }
      if (p && cur > 1) { cur--; render(); window.scrollTo({ top: 0, behavior: 'smooth' }); }
    });
    wiz.addEventListener('input', sync);
    wiz.addEventListener('change', sync);

    // Pilihan jenis laporan
    $$('[data-jenis]', wiz).forEach(function (chip) {
      chip.addEventListener('click', function () {
        var j = chip.dataset.jenis;
        form.elements['jenis'].value = j;
        $$('[data-jenis]', wiz).forEach(function (c) { c.classList.toggle('on', c === chip); });
        $$('[data-only]', wiz).forEach(function (el) {
          var show = el.dataset.only === j;
          el.hidden = !show;
          $$('[data-req-when]', el).forEach(function (r) { r.required = show; });
        });
        $$('[data-text-hilang]', wiz).forEach(function (el) {
          el.textContent = j === 'temuan' ? el.dataset.textTemuan : el.dataset.textHilang;
        });
        $$('[data-ph-hilang]', wiz).forEach(function (el) {
          el.placeholder = j === 'temuan' ? el.dataset.phTemuan : el.dataset.phHilang;
        });
        sync();
      });
    });
    render();
  }

  /* ---------- Upload file: validasi + preview ---------- */
  function initFiles() {
    $$('.drop input[type=file]').forEach(function (input) {
      input.addEventListener('change', function () {
        var drop = input.closest('.drop');
        var nm = $('.nm', drop), msg = $('.file-msg', drop.parentElement) || null;
        var prev = $('img.preview', drop);
        if (prev) { prev.remove(); }
        var f = input.files && input.files[0];
        var err = '';
        if (f) {
          if (['image/png', 'image/jpeg'].indexOf(f.type) === -1) { err = 'Format file harus PNG atau JPG'; }
          else if (f.size > 5 * 1024 * 1024) { err = 'Ukuran file maksimal 5 MB'; }
        }
        if (err) {
          input.value = '';
          if (nm) { nm.textContent = nm.dataset.default; }
          drop.classList.add('err');
          toast(err, 'error');
          if (msg) { msg.textContent = err; }
          input.dispatchEvent(new Event('input', { bubbles: true }));
          return;
        }
        drop.classList.remove('err');
        if (msg) { msg.textContent = ''; }
        if (f) {
          if (nm) { nm.textContent = f.name; }
          var img = document.createElement('img');
          img.className = 'preview';
          img.alt = 'Pratinjau foto';
          img.src = URL.createObjectURL(f);
          drop.insertBefore(img, drop.firstChild);
        } else if (nm) { nm.textContent = nm.dataset.default; }
      });
    });
  }

  /* ---------- Hal-hal kecil ---------- */
  function initMisc() {
    // Tampil/sembunyi kata sandi
    $$('[data-eye]').forEach(function (b) {
      b.addEventListener('click', function () {
        var inp = b.parentElement.querySelector('input');
        inp.type = inp.type === 'password' ? 'text' : 'password';
      });
    });
    // Nomor telepon: hanya angka, +, -, spasi
    $$('input[type=tel]').forEach(function (t) {
      t.addEventListener('input', function () {
        var c = t.value.replace(/[^0-9+\-\s]/g, '');
        if (c !== t.value) { t.value = c; }
      });
    });
    // Kecocokan konfirmasi kata sandi
    var src = $('[data-match-source]'), dst = $('[data-match-target]');
    if (src && dst) {
      var hint = $('[data-match-hint]');
      var check = function () {
        var bad = dst.value.length > 0 && src.value !== dst.value;
        dst.classList.toggle('err', bad);
        if (hint) { hint.textContent = bad ? 'Kata sandi belum sama' : ''; }
      };
      src.addEventListener('input', check);
      dst.addEventListener('input', check);
    }
    // Filter yang langsung terkirim saat dipilih
    $$('[data-autosubmit]').forEach(function (el) {
      el.addEventListener('change', function () { el.form.submit(); });
    });
    // Salin tautan
    $$('[data-copy]').forEach(function (b) {
      b.addEventListener('click', function () {
        var url = window.location.href;
        var done = function () { toast('Tautan laporan disalin.', 'success'); };
        var fail = function () { toast('Tautan tidak dapat disalin. Salin dari bilah alamat.', 'error'); };
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(url).then(done, fail);
        } else { fail(); }
      });
    });
    // Menu samping (mobile)
    var tg = $('.menu-toggle');
    if (tg) {
      tg.addEventListener('click', function () { document.body.classList.toggle('nav-open'); });
      var sc = $('.scrim-nav');
      if (sc) { sc.addEventListener('click', function () { document.body.classList.remove('nav-open'); }); }
    }
    // Esc menutup modal server-side
    var closer = $('[data-close-href]');
    if (closer) {
      document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') { window.location.href = closer.dataset.closeHref; }
      });
    }
    // Alasan penolakan wajib saat menolak klaim
    $$('[data-require-reason]').forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        var ta = document.getElementById(btn.dataset.requireReason);
        if (ta && !ta.value.trim()) {
          e.preventDefault();
          ta.classList.add('err');
          ta.focus();
          toast('Alasan penolakan wajib diisi.', 'error');
        }
      });
    });
    // Scroll-spy landing
    var tabs = $$('.ltab');
    if (tabs.length) {
      var ids = tabs.map(function (t) { return t.getAttribute('href').slice(1); });
      var spy = function () {
        var y = window.scrollY + 120, active = 0;
        ids.forEach(function (id, i) {
          var s = document.getElementById(id);
          if (s && y >= s.offsetTop) { active = i; }
        });
        tabs.forEach(function (t, i) { t.classList.toggle('on', i === active); });
      };
      window.addEventListener('scroll', spy, { passive: true });
      spy();
    }
  }

  /* ---------- Konfirmasi aksi berisiko ---------- */
  function initConfirm() {
    document.addEventListener('submit', function (e) {
      var form = e.target;
      if (!form.dataset || !form.dataset.confirm || form.dataset.confirmed === '1') { return; }
      e.preventDefault();
      var wrap = document.createElement('div');
      wrap.innerHTML =
        '<div class="scrim" data-x></div><div class="modal-wrap"><div class="modal" role="dialog" aria-modal="true">' +
        '<div class="t-h2"></div><p class="p14" style="margin-top:8px"></p>' +
        '<div class="row-between" style="margin-top:24px"><button type="button" class="btn s" data-x>Batal</button>' +
        '<button type="button" class="btn p" data-ok></button></div></div></div>';
      wrap.querySelector('.t-h2').textContent = form.dataset.confirmTitle || 'Konfirmasi';
      wrap.querySelector('p').textContent = form.dataset.confirm;
      wrap.querySelector('[data-ok]').textContent = form.dataset.confirmOk || 'Ya, lanjutkan';
      document.body.appendChild(wrap);
      wrap.querySelector('[data-ok]').focus();
      var close = function () { wrap.remove(); };
      wrap.addEventListener('click', function (ev) {
        if (ev.target.closest('[data-x]')) { close(); }
        if (ev.target.closest('[data-ok]')) { form.dataset.confirmed = '1'; close(); form.requestSubmit(); }
      });
      document.addEventListener('keydown', function esc(ev) {
        if (ev.key === 'Escape') { close(); document.removeEventListener('keydown', esc); }
      });
    });
  }

  /* ---------- Tabel -> kartu di ponsel: isi data-label dari header kolom ---------- */
  function initTables() {
    $$('table.tbl').forEach(function (t) {
      var heads = $$('thead th', t).map(function (th) { return th.textContent.trim(); });
      var hide = $$('thead th', t).map(function (th) { return th.classList.contains('hide-md'); });
      $$('tbody tr', t).forEach(function (tr) {
        $$('td', tr).forEach(function (td, i) {
          td.setAttribute('data-label', heads[i] || '');
          if (hide[i]) { td.classList.add('hide-md'); }
        });
      });
    });
  }

  /* ---------- Menu titik tiga (Edit / Hapus) ---------- */
  function initKebab() {
    var terbuka = null;
    function tutup() {
      if (terbuka) {
        terbuka.menu.hidden = true;
        terbuka.btn.setAttribute('aria-expanded', 'false');
        terbuka = null;
      }
    }
    // Menu memakai position:fixed agar tidak terpotong kartu/tabel
    function posisi(btn, menu) {
      var r = btn.getBoundingClientRect();
      var w = menu.offsetWidth || 160, h = menu.offsetHeight || 96;
      var kiri = Math.min(Math.max(8, r.right - w), window.innerWidth - w - 8);
      var atas = r.bottom + 6;
      if (atas + h > window.innerHeight - 8) { atas = Math.max(8, r.top - h - 6); }
      menu.style.position = 'fixed';
      menu.style.left = kiri + 'px';
      menu.style.top = atas + 'px';
    }
    document.addEventListener('click', function (e) {
      var btn = e.target.closest('.kbtn');
      if (btn) {
        e.preventDefault();
        var menu = btn.parentElement.querySelector('.kmenu');
        var sudah = terbuka && terbuka.menu === menu;
        tutup();
        if (!sudah) {
          menu.hidden = false;
          btn.setAttribute('aria-expanded', 'true');
          posisi(btn, menu);
          terbuka = { btn: btn, menu: menu };
        }
        return;
      }
      if (terbuka && !e.target.closest('.kmenu')) { tutup(); }
    });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') { tutup(); } });
    window.addEventListener('scroll', tutup, true);
    window.addEventListener('resize', tutup);
  }

  /* ---------- Perbesar foto: klik pratinjau membuka ukuran asli ---------- */
  function initZoom() {
    document.addEventListener('click', function (e) {
      var a = e.target.closest('[data-zoom]');
      if (!a) { return; }
      e.preventDefault();
      var box = document.createElement('div');
      box.className = 'lightbox';
      box.setAttribute('role', 'dialog');
      box.setAttribute('aria-label', 'Foto ukuran asli');
      var img = document.createElement('img');
      img.src = a.getAttribute('href');
      img.alt = 'Foto ukuran asli';
      var x = document.createElement('button');
      x.type = 'button';
      x.className = 'lb-x';
      x.setAttribute('aria-label', 'Tutup');
      x.innerHTML = ICON_X;
      box.appendChild(img);
      box.appendChild(x);
      var tutup = function () { box.remove(); document.removeEventListener('keydown', esc); };
      var esc = function (ev) { if (ev.key === 'Escape') { tutup(); } };
      box.addEventListener('click', tutup);
      document.addEventListener('keydown', esc);
      document.body.appendChild(box);
    });
  }

  /* ---------- Gambar yang gagal dimuat diganti ikon ---------- */
  function gantiGambarRusak(img) {
    if (!img.closest('.pic, .ph, .thumb')) { return; }
    var box = document.createElement('span');
    box.className = 'img-fallback';
    box.innerHTML = '<svg class="ic s28" aria-hidden="true"><use href="#i-box"></use></svg>';
    img.replaceWith(box);
  }
  function initGambar() {
    document.addEventListener('error', function (e) {
      if (e.target && e.target.tagName === 'IMG') { gantiGambarRusak(e.target); }
    }, true);
    // Gambar yang sudah gagal sebelum skrip ini aktif
    $$('img').forEach(function (img) {
      if (img.complete && img.naturalWidth === 0) { gantiGambarRusak(img); }
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    initTables();
    initGambar();
    initZoom();
    initKebab();
    showFlash();
    initForms();
    initWizard();
    initFiles();
    initMisc();
    initConfirm();
  });
})();
