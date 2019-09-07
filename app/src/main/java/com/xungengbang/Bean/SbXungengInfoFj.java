package com.xungengbang.Bean;

public class SbXungengInfoFj {
    private static final long serialVersionUID = 1L;
    private String fjId; // 巡更信息附件ID
    private String fjName; // 附件名称
    private Long fjDaxiao; // 附件大小
    private String fjUrl; // 附件地址，一般存储文件在磁盘的地址
    private String tableId;// 储存场所ID

    public String getFjId() {
        return fjId;
    }

    public void setFjId(String fjId) {
        this.fjId = fjId;
    }


    public Long getFjDaxiao() {
        return fjDaxiao;
    }

    public void setFjDaxiao(Long fjDaxiao) {
        this.fjDaxiao = fjDaxiao;
    }

    public String getFjUrl() {
        return fjUrl;
    }

    public void setFjUrl(String fjUrl) {
        this.fjUrl = fjUrl;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getFjName() {
        return fjName;
    }

    public void setFjName(String fjName) {
        this.fjName = fjName;
    }

}
