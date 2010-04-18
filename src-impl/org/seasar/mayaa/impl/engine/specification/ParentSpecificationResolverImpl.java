package org.seasar.mayaa.impl.engine.specification;

import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.ParentSpecificationResolver;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.provider.ProviderUtil;

public class ParentSpecificationResolverImpl extends ParameterAwareImpl implements ParentSpecificationResolver {

    private static final long serialVersionUID = 8831606528015900173L;

    public Specification getParentSpecification(Specification spec) {
        if (spec instanceof Page) {
            return ProviderUtil.getEngine();
        } else if (spec instanceof Template) {
            return ((Template) spec).getPage();
        }
        return null;
    }

}
