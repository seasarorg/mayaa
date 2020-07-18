package org.seasar.mayaa.impl.management;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class JMXUtil {
    static void register(Object mbean, ObjectName objectName) {
        if (mbean == null) {
            throw new IllegalArgumentException();
        }

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer(); // MBeanServer を取得

            // 重複登録されるとInstanceAlreadyExistsExceptionが発生するので事前に削除しておく。
            try {
                mBeanServer.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                // Ignore
            }
            mBeanServer.registerMBean(mbean, objectName);
        } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
                    System.err.println(e);
        } catch (InstanceAlreadyExistsException e) {
        }
    }

}