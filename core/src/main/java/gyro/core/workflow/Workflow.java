package gyro.core.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.core.GyroUI;
import gyro.core.resource.Diff;
import gyro.core.resource.NodeEvaluator;
import gyro.core.resource.Resource;
import gyro.core.resource.RootScope;
import gyro.core.resource.Scope;
import gyro.core.resource.State;
import gyro.lang.ast.Node;
import gyro.lang.ast.block.ResourceNode;

public class Workflow {

    private final RootScope rootScope;
    private final String name;
    private final String forType;
    private final List<Stage> stages = new ArrayList<>();

    public Workflow(Scope parent, ResourceNode node) {
        rootScope = parent.getRootScope();
        Scope scope = new Scope(parent);
        NodeEvaluator evaluator = rootScope.getEvaluator();

        for (Node item : node.getBody()) {
            if (item instanceof ResourceNode) {
                ResourceNode rn = (ResourceNode) item;

                if (rn.getType().equals("stage")) {
                    Stage stage = new Stage(scope, rn);

                    stages.add(stage);
                    continue;
                }
            }

            evaluator.visit(item, scope);
        }

        name = (String) evaluator.visit(node.getName(), parent);
        forType = (String) scope.get("for-type");
    }

    public String getName() {
        return name;
    }

    public String getForType() {
        return forType;
    }

    private RootScope copyCurrentRootScope() throws Exception {
        RootScope current = rootScope.getCurrent();
        RootScope scope = new RootScope(
            current.getFile(),
            current.getBackend(),
            null,
            current.getLoadFiles());

        scope.load();

        return scope;
    }

    public void execute(
            GyroUI ui,
            State state,
            Resource currentResource,
            Resource pendingResource)
            throws Exception {

        int stagesSize = stages.size();

        if (stagesSize == 0) {
            throw new IllegalArgumentException("No stages!");
        }

        int stageIndex = 0;

        do {
            Stage stage = stages.get(stageIndex);
            String stageName = stage.getName();

            ui.write("\n@|magenta %d Executing %s stage|@\n", stageIndex + 1, stageName);

            if (ui.isVerbose()) {
                ui.write("\n");
            }

            ui.indent();

            try {
                stageName = stage.execute(ui, state, currentResource, pendingResource, copyCurrentRootScope(), copyCurrentRootScope());

            } finally {
                ui.unindent();
            }

            if (stageName == null) {
                ++stageIndex;

            } else {
                stageIndex = -1;

                for (int i = 0; i < stagesSize; ++i) {
                    Stage s = stages.get(i);

                    if (s.getName().equals(stageName)) {
                        stageIndex = i;
                        break;
                    }
                }

                if (stageIndex < 0) {
                    throw new IllegalArgumentException(String.format(
                            "No stage named [%s]!",
                            stageName));
                }
            }

        } while (stageIndex < stagesSize);

        RootScope current = copyCurrentRootScope();

        RootScope pending = new RootScope(
            rootScope.getFile(),
            rootScope.getBackend(),
            current,
            rootScope.getLoadFiles());

        pending.load();

        Set<String> diffFiles = state.getDiffFiles();

        Diff diff = new Diff(
            current.findResourcesIn(diffFiles),
            pending.findResourcesIn(diffFiles));

        diff.diff();

        if (diff.write(ui)) {
            if (ui.readBoolean(Boolean.TRUE, "\nFinalize %s workflow?", name)) {
                ui.write("\n");
                diff.executeCreateOrUpdate(ui, state);
                diff.executeReplace(ui, state);
                diff.executeDelete(ui, state);

            } else {
                throw new RuntimeException("Aborted!");
            }
        }
    }

}
