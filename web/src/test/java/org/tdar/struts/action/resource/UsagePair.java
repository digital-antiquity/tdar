package org.tdar.struts.action.resource;

import org.tdar.utils.Pair;

public class UsagePair extends Pair<Long, Long> {
    private static final long serialVersionUID = 3493370798695227648L;

    public UsagePair(Long first, Long second) {
        super(first, second);
    }

    public long files() {
        return getFirst();
    }

    public long bytes() {
        return getSecond();
    }
}
