package io.apitomy.umg.base.union;

import io.apitomy.umg.base.Any;
import io.apitomy.umg.base.Visitable;

public interface Union extends Any, Visitable {

    public Object unionValue();

    public boolean isEntity();

    public boolean isEntityList();

    public boolean isEntityMap();

}
