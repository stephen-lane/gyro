package gyro.core.auth;

import java.util.List;

import gyro.core.GyroException;
import gyro.core.directive.DirectiveProcessor;
import gyro.core.resource.DiffableScope;
import gyro.lang.ast.block.DirectiveNode;

public class UsesCredentialsDirectiveProcessor extends DirectiveProcessor<DiffableScope> {

    @Override
    public String getName() {
        return "uses-credentials";
    }

    @Override
    public void process(DiffableScope scope, DirectiveNode node) {
        List<Object> arguments = evaluateArguments(scope, node);

        if (arguments.size() != 1) {
            throw new GyroException("@uses-credentials directive only takes 1 argument!");
        }

        scope.getSettings(CredentialsSettings.class).setUseCredentials((String) arguments.get(0));
    }

}