package org.gengine.content.dropwizard.views;

import io.dropwizard.views.View;

/**
 * Status view placeholder
 *
 */
public class NodeView extends View
{
    private static final String TEMPLATE = "status.mustache";

    public NodeView()
    {
        super(TEMPLATE);
    }

}
