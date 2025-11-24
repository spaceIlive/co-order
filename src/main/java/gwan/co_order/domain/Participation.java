package gwan.co_order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "participations")
@Getter @Setter
public class Participation {

    @Id @GeneratedValue
    @Column(name = "participation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status; // JOINED, CANCELLED

    // 무조건 스냅샷으로 저장해둬야함 주문 도중에 주소 바꿔도 참여자 정보는 바뀌지 않아야함
    @Embedded
    private Address address;

    public static Participation createParticipation(Member member, Post post, Address address) {
        Participation participation = new Participation();
        participation.member = member;
        participation.post = post;
        participation.address = address;
        participation.status = ParticipationStatus.JOINED;
        return participation;
    }
}