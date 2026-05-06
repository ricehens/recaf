package recaf.main;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Error handler for the Recaf compiler.
 */
public class RecafErrorHandler {

    private final Queue<RecafError> errors;

    /**
     * Constructs first new instance.
     */
    public RecafErrorHandler() {
        errors = new PriorityQueue<>();
    }

    /**
     * Reports an error into the queue.
     *
     * @param file               file text
     * @param line               line number
     * @param charPositionInLine column number
     * @param message            message to be stored
     */
    public void error(String file, int line, int charPositionInLine, String message) {
        errors.offer(new RecafError(file, line, charPositionInLine, message));
    }

    /**
     * Returns true if the queue of errors is not empty.
     *
     * @return whether the error queue is nonempty
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Prints the queue of errors to standard error.
     * Empties the queue.
     */
    public void printErrors() {
        Queue<RecafError> q = errors;
        while (!q.isEmpty()) {
            System.err.println(q.poll());
        }
    }

}
