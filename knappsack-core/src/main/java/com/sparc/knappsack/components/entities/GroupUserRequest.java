package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.Status;

import javax.persistence.*;

/**
 * A GroupUserRequest is a request a user makes in order to joint a group.  Once the user request is accepted or denied, this request is removed.
 */
@Entity
@Table(name = "GROUP_USER_REQUEST")
public class GroupUserRequest extends BaseEntity implements Notifiable {

    private static final long serialVersionUID = 5339644131539411920L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "ORG_GROUP_ID")
    private Group group;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.GROUP_USER_REQUEST;
    }
}
