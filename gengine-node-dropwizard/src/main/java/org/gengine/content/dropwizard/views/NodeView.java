package org.gengine.content.dropwizard.views;

import io.dropwizard.views.View;

public class NodeView extends View
{
    private static final String TEMPLATE = "status.mustache";

    public NodeView()
    {
        super(TEMPLATE);
    }

}
