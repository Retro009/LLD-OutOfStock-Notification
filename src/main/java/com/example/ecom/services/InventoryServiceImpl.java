package com.example.ecom.services;

import com.example.ecom.adapters.SendGridAdapter;
import com.example.ecom.exceptions.ProductNotFoundException;
import com.example.ecom.libraries.Sendgrid;
import com.example.ecom.models.Inventory;
import com.example.ecom.models.Notification;
import com.example.ecom.models.NotificationStatus;
import com.example.ecom.models.Product;
import com.example.ecom.repositories.InventoryRepository;
import com.example.ecom.repositories.NotificationRepository;
import com.example.ecom.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService{

    private InventoryRepository inventoryRepository;
    private ProductRepository productRepository;
    private NotificationRepository notificationRepository;
    private SendGridAdapter sendGridAdapter;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository, NotificationRepository notificationRepository, SendGridAdapter sendGridAdapter) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.notificationRepository = notificationRepository;
        this.sendGridAdapter = sendGridAdapter;
    }

    @Override
    public Inventory updateInventory(int productId, int quantity) throws ProductNotFoundException {
        Product product = this.productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Product not found"));
        Optional<Inventory> inventoryOptional = this.inventoryRepository.findByProduct(product);
        Inventory inventory;
        if(inventoryOptional.isEmpty()){
            inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(quantity);
            inventory = inventoryRepository.save(inventory);
        }else{
            inventory = inventoryOptional.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory = inventoryRepository.save(inventory);
        }
        if(inventory.getQuantity()>0){
            List<Notification> notifications = notificationRepository.findByProduct(inventory.getProduct());
            for(Notification notification:notifications){
                if(notification.getStatus() == NotificationStatus.PENDING){
                    sendGridAdapter.sendEmail(notification.getUser().getEmail(), product.getName() + " back in stock!", "Dear " + notification.getUser().getName() + ", " + product.getName() + " is now back in stock. Grab it ASAP!");
                    notification.setStatus(NotificationStatus.SENT);
                    notificationRepository.save(notification);
                }
            }
        }
        return inventory;
    }
}
