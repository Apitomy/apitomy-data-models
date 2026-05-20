package io.apitomy.umg.pipe;

import java.util.ArrayList;
import java.util.List;

public class Pipeline {

    private List<Stage> stages = new ArrayList<>();

    public Pipeline() {
    }

    public void addStage(Stage stage) {
        stages.add(stage);
    }

    public void run(GeneratorState state) {
        for (Stage stage : stages) {
            stage.process(state);
        }
    }
}
