package com.kuochan.es.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @author 贝壳
 * @date 2019/7/22 3:33 PM
 */
@Data
public class SearchResultVO implements Serializable {
    private static final long serialVersionUID = -7398556039041212941L;

    private Long total;
    private List<PostVO> data;
}
