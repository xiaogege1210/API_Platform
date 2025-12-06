
package com.example.demo.dto;

import lombok.Data;
import java.util.List;

/**
 * 树形文件节点DTO
 */
@Data
public class TreeNodeDto {
    /** 节点ID（可用于前端标识，这里用文件路径的哈希值） */
    private String id;
    /** 节点名称（文件名/文件夹名） */
    private String name;
    /** 节点类型：file-文件，dir-文件夹 */
    private String type;
    /** 文件路径（完整路径） */
    private String path;
    /** 子节点（仅文件夹有） */
    private List<TreeNodeDto> children;

    // 构造方法（简化创建）
    public TreeNodeDto(String name, String type, String path) {
        this.id = String.valueOf(path.hashCode()); // 用路径哈希作为唯一ID
        this.name = name;
        this.type = type;
        this.path = path;
    }
}