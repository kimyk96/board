package com.pistis.board.domain.board.entity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BOARD")
public class Board {
    @Id
    @Column(name = "bno")
    private Long bno;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "writer")
    private String writer;

    @Column(name = "write_time")
    private LocalDateTime writeTime;

    @Column(name = "read_cnt")
    private Integer readCnt;

    @Column(name = "comment_cnt")
    private Integer commentCnt;

    @Column(name = "good_cnt")
    private Integer goodCnt;

    @Column(name = "bad_cnt")
    private Integer badCnt;

}
//bno number(6) constraint board_pk_bno primary key,
//title varchar2(100 char),
//content clob,
//writer varchar2(10 char),
//writeTime date default sysdate,
//readCnt number(3) default 0,
//commentCnt number(3) default 0,
//goodCnt number(4) default 0,
//badCnt number(4) default 0