package backend.clockin.pojo.faceRec;

/**
 * 返回人脸识别对象
 *
 * @author panjun
 * @version 1.0
 *          <p>
 *          <p>修订人		修订时间			描述信息
 *          <p>-----------------------------------------------------
 *          <p>panjun		2018/11/8		初始创建
 */
public class PersonsVo {

    /** 名称 */
    private String name;

    /** uid*/
    private String uid;

    /** 类别 */
    private String type;

    /** 底库照片 */
    private String faceUrl;

    /** 分数 */
    private Double score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
