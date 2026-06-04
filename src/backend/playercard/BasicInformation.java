package backend.playercard;

/*
 * coc 角色卡基本数据
*/
public class BasicInformation {
    private String name;
    private String occupation;
    private int age;
    private String gender;
    private String residence;
    private String birthplace;
    private String avatarPath;

    public BasicInformation(String name) {
        setName(name);
        this.occupation = "";
        this.age = 0;
        this.gender = "";
        this.residence = "";
        this.birthplace = "";
        this.avatarPath = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("角色姓名不能为空");
        }
        this.name = name;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        CardValueValidator.checkNonNegative(age, "年龄");
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    // url for avatar image
    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }
}
