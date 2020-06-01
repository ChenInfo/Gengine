package org.gengine.health;

/**
 * Defines actions to be taken when a service's dependent component is unavailable.
 *
 */
public interface ComponentUnavailableAction
{

    /**
     * Executes the action using the given exception
     *
     * @param e the exception
     */
    public void execute(Throwable e);

}
