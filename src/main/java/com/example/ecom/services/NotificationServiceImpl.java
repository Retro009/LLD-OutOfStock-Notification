package com.example.ecom.services;

import com.example.ecom.exceptions.*;
import com.example.ecom.models.*;
import com.example.ecom.repositories.InventoryRepository;
import com.example.ecom.repositories.NotificationRepository;
import com.example.ecom.repositories.ProductRepository;
import com.example.ecom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class NotificationServiceImpl implements NotificationService{
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Override
    public Notification registerUser(int userId, int productId) throws UserNotFoundException, ProductNotFoundException, ProductInStockException {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User Not Found"));
        Product product = productRepository.findById(productId).orElseThrow(()-> new ProductNotFoundException("Product not found"));
        Optional<Inventory> inventory = inventoryRepository.findByProduct(product);
        if(inventory.isPresent() && inventory.get().getQuantity()>0)
            throw new ProductInStockException("Already have stock for the product");

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setProduct(product);
        notification.setStatus(NotificationStatus.PENDING);

        return notificationRepository.save(notification);
    }

    @Override
    public void deregisterUser(int userId, int notificationId) throws UserNotFoundException, NotificationNotFoundException, UnAuthorizedException {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User Not Found"));
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(()->new NotificationNotFoundException("Notification not Found"));
        if(!user.equals(notification.getUser()))
            throw new UnAuthorizedException("Notification Doesn't belong to the user");
        notificationRepository.delete(notification);
    }
}
