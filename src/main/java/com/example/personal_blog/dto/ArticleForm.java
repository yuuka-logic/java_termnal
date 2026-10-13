package com.example.personal_blog.dto;

import com.example.personal_blog.entity.Article;

import java.util.List;

/**
 * 文章新增/修改入参。
 * 继承 Article 以复用全部字段，额外携带 tagIds —— 它不是 article 表的列，
 * 而是要写入 article_tag 中间表的标签集合。
 */
public class ArticleForm extends Article {

    /** 标签ID集合。传空数组表示清空标签；传 null 表示不修改标签 */
    private List<Integer> tagIds;

    public List<Integer> getTagIds() { return tagIds; }
    public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }
}
