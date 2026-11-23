package lostfound.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

class PageViewTest {

    @Test
    void membagiHalamanDenganBenar() {
        List<Integer> data = IntStream.rangeClosed(1, 21).boxed().toList();
        PageView<Integer> p3 = PageView.of(data, 3, 8);
        assertThat(p3.items()).containsExactly(17, 18, 19, 20, 21);
        assertThat(p3.totalPages()).isEqualTo(3);
        assertThat(p3.from()).isEqualTo(17);
        assertThat(p3.to()).isEqualTo(21);
        assertThat(p3.adaBerikutnya()).isFalse();
        assertThat(p3.adaSebelumnya()).isTrue();
    }

    @Test
    void halamanDiLuarBatasDirapikanDanDaftarKosongAman() {
        PageView<Integer> kosong = PageView.of(List.of(), 9, 8);
        assertThat(kosong.items()).isEmpty();
        assertThat(kosong.page()).isEqualTo(1);
        assertThat(kosong.total()).isZero();
        assertThat(kosong.from()).isZero();
    }
}
