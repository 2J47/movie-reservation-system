package com.myproject.movie_ticket.domain.user;

import com.myproject.movie_ticket.domain.user.enums.UserRole;
import com.myproject.movie_ticket.domain.user.enums.UserStatus;
import com.myproject.movie_ticket.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP, status = 'WITHDRAWN' WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; //USER, ADMIN 일단은 유저만

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status; //ACTIVE, INACTIVE

    @Builder
    public User(String email, String password, String name, String phoneNumber, UserRole role, UserStatus status) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : UserRole.USER; // 기본값 설정
        this.status = status != null ? status : UserStatus.ACTIVE; // 기본값 설정
    }

    //정보 수정
    public void updateProfile(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
    //비번 변경
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
    //회원 탈퇴
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.name = "탈퇴한 사용자";
        this.phoneNumber = "WITHDRAWN_" + this.id;
        this.email = "WITHDRAWN_" + this.id + "@masked.local";
        this.recordDeletedAt();
    }

}
