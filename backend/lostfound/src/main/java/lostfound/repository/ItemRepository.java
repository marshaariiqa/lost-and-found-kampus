package lostfound.repository;

import lostfound.model.Item;
import lostfound.model.ItemStatus;
import lostfound.model.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByType(ItemType type);
    List<Item> findByStatus(ItemStatus status);
    List<Item> findByUserId(Long userId);
}