package gwan.co_order.service;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Member;
import gwan.co_order.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    private Member member;
    private Address address;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setName("yoon");
        member.setPassword("123");
        address = new Address("서울시 강남구", 1.0, 1.0);
        member.setAddress(address);
    }

    @Test
    @DisplayName("회원가입")
    void join() {
        //when
        Long return_id = memberService.join(member);
        // 이게 다 끝나도 DB에는 안들어감(jpa가 인서트문 실행x) em이 들고있는데 굳이 flush할필요 없다생각 왜냐면 transactional 어노테이션 때문에 만약에 보고싶으면 em.flush()해주기

        //then
        assertEquals(member, memberRepository.findMemberById(return_id));
    }

    @Test
    @DisplayName("중복 회원 예외")
    void duplicatedName() {
        //given
        Member member2 = new Member();
        member2.setName("yoon");
        member2.setPassword("123");
        member2.setAddress(address);

        //when
        memberService.join(member);

        //then
        assertThrows(IllegalStateException.class, () -> memberService.join(member2));
    }

    @Test
    @DisplayName("회원 이름으로 조회")
    void findMemberByName() {
        //given
        memberService.join(member);

        //when
        Member foundMember = memberService.findMemberByName("yoon");

        //then
        assertNotNull(foundMember);
        assertEquals("yoon", foundMember.getName());
    }

    @Test
    @DisplayName("회원 ID로 조회")
    void findMemberById() {
        //given
        Long memberId = memberService.join(member);

        //when
        Member foundMember = memberService.findMemberById(memberId);

        //then
        assertNotNull(foundMember);
        assertEquals(memberId, foundMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회")
    void findMemberById_NotFound() {
        //when & then
        Member foundMember = memberService.findMemberById(999L);
        assertNull(foundMember);
    }

    @Test
    @DisplayName("회원 주소 조회")
    void findAddress() {
        //given
        Long memberId = memberService.join(member);

        //when
        Address foundAddress = memberService.findAddress(memberId);

        //then
        assertNotNull(foundAddress);
        assertEquals("서울시 강남구", foundAddress.getAddress());
    }

    @Test
    @DisplayName("회원 주소 업데이트")
    void updateAddress() {
        //given
        Long memberId = memberService.join(member);
        Address newAddress = new Address("서울시 서초구", 2.0, 2.0);

        //when
        memberService.updateAddress(memberId, newAddress);

        //then
        Address updatedAddress = memberService.findAddress(memberId);
        assertEquals("서울시 서초구", updatedAddress.getAddress());
        assertEquals(2.0, updatedAddress.getLatitude());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        //given
        memberService.join(member);

        //when
        Member loggedInMember = memberService.login("yoon", "123");

        //then
        assertNotNull(loggedInMember);
        assertEquals("yoon", loggedInMember.getName());
    }

    @Test
    @DisplayName("로그인 실패 - 등록되지 않은 아이디")
    void login_NotFound() {
        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            memberService.login("nonexistent", "123");
        });
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void login_WrongPassword() {
        //given
        memberService.join(member);

        //when & then
        assertThrows(IllegalStateException.class, () -> {
            memberService.login("yoon", "wrong");
        });
    }
}