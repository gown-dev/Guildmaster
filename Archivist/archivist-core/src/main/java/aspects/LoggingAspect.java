package aspects;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Logged;

@Aspect
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(logged)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint, Logged logged) throws Throwable {
        
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String description = logged.value().isEmpty() ? "Execution" : logged.value();

        if (logged.logEntry()) {
            logger.info("-> [{}]: {} pour {}.{}() avec comme arguments : {}", description, "Début", className, methodName, Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            if (logged.logExit() && logged.logSuccess()) {
            	logger.info("<- [{}]: {} pour {}.{}() avec résultat: {} (Durée: {} ms)", description, "Succès", className, methodName, result, duration);
            }
            
            return result;
            
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (logged.logExit() && logged.logFailure()) {
                logger.error("!! [{}]: {} pour {}.{}() avec exception: {} (Durée: {} ms)", description, "Échec", className, methodName, e.getMessage(), duration);
            }
            
            throw e;
        }
    }
}