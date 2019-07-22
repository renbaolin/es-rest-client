package com.kuochan.es.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * @author 贝壳
 * @date 2019/7/22 3:23 PM
 */
@Data
public class PostVO implements Serializable {
    private static final long serialVersionUID = -3438795648699901426L;

    private Long id;
    private Integer type;
    private String subject;
    private String message;
    private Long fid;
    private Integer status;
}
