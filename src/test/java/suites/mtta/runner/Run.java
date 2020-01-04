package suites.mtta.runner;

import org.testng.annotations.Test;
import support.MTT;

import java.util.Calendar;

public class Run {
    private int maxInstances = 20;

    @Test
    public void runnable1(){
        new MTT().run(
                "/src/test/java/suites/mtta/stepdefs/",
                "/src/test/java/suites/mtta/features/",
                maxInstances
        );
    }

    @Test
    public void runnable2(){
        new MTT().run(
                "/src/test/java/suites/mtta/stepdefs/",
                "/src/test/java/suites/mtta/features/",
                maxInstances
        );
    }

    @Test
    public void runnable3(){
        new MTT().run(
                "/src/test/java/suites/mtta/stepdefs/",
                "/src/test/java/suites/mtta/features/",
                maxInstances
        );
    }

    @Test
    public void runnable4(){
        new MTT().run(
                "/src/test/java/suites/mtta/stepdefs/",
                "/src/test/java/suites/mtta/features/",
                maxInstances
        );
    }

    @Test
    public void runnable5(){
        new MTT().run(
                "/src/test/java/suites/mtta/stepdefs/",
                "/src/test/java/suites/mtta/features/",
                maxInstances
        );
    }
}
