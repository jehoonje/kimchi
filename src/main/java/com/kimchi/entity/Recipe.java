package com.kimchi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipes")
@Getter
@Setter
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예: 식품안전나라의 RCP_SEQ
    private Integer rcpSeq;

    // 메뉴 이름(영어)
    private String name;

    // 이미지(예: ATT_FILE_NO_MK)
    private String image;

    // 재료(영어 번역)
    @Column(length = 2000)
    private String ingredients;

    // 단계별 설명 (예: manual01~manual19 -> 하나의 긴 텍스트로 합칠 수도, 컬럼 여러개 둘 수도)
    @Column(length = 2000)
    private String manual01;
    @Column(length = 2000)
    private String manual02;
    @Column(length = 2000)
    private String manual03;
    // 필요하면 ~ manual19까지...

    // TIP(영어)
    @Column(length = 2000)
    private String tip;

    // 추가 영양정보, WGT, ENG, etc... 필요하다면 필드 추가
}
