package io.upgradepilot.scan.analysis;

import java.util.List;

/** Add a new check by implementing this and annotating it with @Component. */
public interface UpgradeRule {
    List<Finding> check(ProjectContext ctx);
}
