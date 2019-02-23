package workspace;

public class Test {
    boolean flag = false;

    public void doT(){
        this.flag = true;
        return;
    }

    public static void main(String[] args) {
        Test test = new Test();
        test.doT();
        System.out.println(test.flag);
    }
}
