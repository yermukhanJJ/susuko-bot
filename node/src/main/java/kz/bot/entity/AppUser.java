package kz.bot.entity;

import kz.bot.entity.enums.UserState;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private Long telegramUserId;
    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Boolean isActive;
    @Enumerated(EnumType.STRING)
    private UserState userState;

}
