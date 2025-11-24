package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name ="posts")
@Getter @Setter
public class Post {
    @Id @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    //한사람이 여러포스트 작성가능 but 한포스트는 여러사람을 가지지 못함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_member_id")
    private Member host; // 작성자

    //호스트 위치
    @Embedded
    private Address address;

    // 어떤 가게에서 시키는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private int minParticipants;
    private int currentParticipants;
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    public static Post createPost(Member host, Store store, Address address, int minParticipants, LocalDateTime deadline) {
        Post post = new Post();
        post.host = host;
        post.store = store;
        post.address = address;
        post.minParticipants = minParticipants;
        post.deadline = deadline;
        post.status = PostStatus.OPEN;
        post.currentParticipants = 0;
        return post;
    }

    public void addParticipant() {
        this.currentParticipants++;
    }

    public void removeParticipant() {
        this.currentParticipants--;
    }
}