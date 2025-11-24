package gwan.co_order.service;

import gwan.co_order.domain.Address;
import gwan.co_order.domain.Member;
import gwan.co_order.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //밑에 부분 위에 @RequiredArgsConstructor 하나면됨
    /*//스프링이 최신버전에서는 생성자가 하나면 Autowired없이 자동으로 인젝션해줌
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }*/

    //회원가입
    //기본적으로 readOnly가 false 임
    @Transactional
    public Long join (Member member) {
        validateDuplicateName(member);
        memberRepository.saveMember(member);
        return member.getId();
    }

    private void validateDuplicateName(Member member) {
        //진짜 만약에 만에하나 동시에 가입을 해서 이 테스트를 둘다 통과하는 경우가 발생할수있는데 최후의 방어로 DB에서 네임(닉네임)에 유니크 조건을 걸어줘야함
        if (memberRepository.existsByName(member.getName())) {
            throw new IllegalStateException("이미 존재하는 이름입니다.");
        }
    }
    @Transactional(readOnly = true)
    public Member findMemberByName(String name) {
        return memberRepository.findMemberByName(name);
    }

    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findMemberById(memberId);
    }

    @Transactional(readOnly = true)
    public Address findAddress(Long memberId) {
        return memberRepository.findAddress(memberId);
    }

    @Transactional
    public void updateAddress(Long memberId, Address address) {
        memberRepository.updateAddress(memberId, address);
    }

    @Transactional(readOnly = true)
    public Member login(String name, String password) {
        Member member = memberRepository.findMemberByNameForLogin(name);
        
        if (member == null) {
            throw new IllegalArgumentException("등록되지 않은 아이디입니다.");
        }
        
        if (!member.getPassword().equals(password)) {
            throw new IllegalStateException("비밀번호가 틀렸습니다.");
        }
        
        return member;
    }
}