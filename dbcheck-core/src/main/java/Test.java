import io.mycat.db.autotest.utils.BeanUtils;

import java.io.Serializable;

/**
 * Created by qiank on 2017/2/10.
 */
public class Test {

    public static class Test2 implements Serializable {
        private String id;
        private Test3 test3;

        public Test2(String id, Test3 test3) {
            this.id = id;
            this.test3 = test3;
        }

        public Test2() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Test3 getTest3() {
            return test3;
        }

        public void setTest3(Test3 test3) {
            this.test3 = test3;
        }
    }

    public static class Test3 implements Serializable {

        private String id;
        private String name;

        public Test3(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Test3() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        Test3 test3 = new Test3("3","test3");
        Test2 test2 = new Test2("2",test3);
        Test2 test21 = BeanUtils.cloneTo(test2);
        test21.getTest3().setName("test31");

        System.out.println(test2.getTest3().getName());
        System.out.println(test21.getTest3().getName());
    }

}
