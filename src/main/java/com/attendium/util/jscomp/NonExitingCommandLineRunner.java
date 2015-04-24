package com.attendium.util.jscomp;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

public class NonExitingCommandLineRunner extends CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(NonExitingCommandLineRunner.class);

    protected static class ExitException extends SecurityException {

        public final int status;

        public ExitException(final int status) {

            this.status = status;
        }
    }

    private static class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {}

        @Override
        public void checkPermission(Permission perm, Object context) {}

        @Override
        public void checkExit(final int status) {

            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    @Override
    protected com.google.javascript.jscomp.Compiler createCompiler() {

        final Compiler compiler = super.createCompiler();
        compiler.disableThreads();
        return compiler;
    }

    public NonExitingCommandLineRunner(final String[] args) {

        super(args);
    }

    public static void main(final String[] args) {

        final StringBuilder argsString = new StringBuilder("Compiling with args:");
        for (final String arg : args) {

            argsString.append(' ');
            argsString.append(arg);
        }
        LOGGER.info(argsString.toString());

        final SecurityManager securityManager = System.getSecurityManager();
        System.setSecurityManager(new NoExitSecurityManager());

        try {

            NonExitingCommandLineRunner runner = new NonExitingCommandLineRunner(args);

            if (runner.shouldRunCompiler()) runner.run();
            if (runner.hasErrors()) LOGGER.error("Error running compiler");
        } catch(final ExitException e) {

            if (e.status == -1) LOGGER.error("Error running compiler");
        }

        System.setSecurityManager(securityManager);
    }
}
