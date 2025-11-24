package gwan.co_order.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import gwan.co_order.service.OrderService;
import gwan.co_order.service.PostService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PostClosureScheduler {
    
    private final PostService postService;
    private final OrderService orderService;
    
    @Scheduled(fixedDelay = 10000)
    public void checkAndProcessExpiredPosts() {
        LocalDateTime now = LocalDateTime.now();
        
        postService.processExpiredOpenPosts(now);
        orderService.createOrdersFromExpiredPosts(now);
    }
}