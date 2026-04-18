package com.payment.inventory.repository;

import com.payment.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Inventory repository with PESSIMISTIC LOCKING support.
 * 
 * Why pessimistic locking?
 * - Inventory updates are high-contention operations
 * - Multiple orders can try to reserve the same product stock simultaneously
 * - Pessimistic lock (SELECT ... FOR UPDATE) serializes access at the DB level
 * - This prevents overselling (race condition where available=1 but 2 orders reserve it)
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /** Find inventory by product ID (no lock — for read-only queries) */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Find inventory by product ID with PESSIMISTIC_WRITE lock.
     * 
     * Translates to: SELECT * FROM inventory WHERE product_id = ? FOR UPDATE
     * 
     * This acquires a row-level exclusive lock:
     * - Other transactions trying to read FOR UPDATE will BLOCK until this lock is released
     * - Prevents concurrent modifications to the same inventory row
     * - Lock is released when the enclosing transaction commits or rolls back
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);
}
