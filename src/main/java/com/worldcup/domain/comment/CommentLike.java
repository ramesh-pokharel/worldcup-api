package com.worldcup.domain.comment;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "comment_likes")
@IdClass(CommentLike.PK.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "comment_id")
    private Long commentId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long userId;
        private Long commentId;
    }
}
