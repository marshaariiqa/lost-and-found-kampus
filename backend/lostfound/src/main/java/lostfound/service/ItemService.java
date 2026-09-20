package lostfound.service;

import lostfound.dto.ItemDTO;
import lostfound.model.Item;
import lostfound.model.User;
import lostfound.repository.ItemRepository;
import lostfound.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Item createItem(ItemDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Item item = new Item(
                dto.getTitle(),
                dto.getDescription(),
                dto.getLocation(),
                dto.getType(),
                user,
                dto.getImageUrl()
        );
        return itemRepository.save(item);
    }

    public Item updateItem(Long id, ItemDTO dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Laporan barang tidak ditemukan"));

        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLocation(dto.getLocation());
        item.setType(dto.getType());
        if (dto.getStatus() != null) {
            item.setStatus(dto.getStatus());
        }
        item.setImageUrl(dto.getImageUrl());

        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}