/*
 * Copyright (c) 2004 - 2006, Stephen Kelvin Friedrich. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list
 *   of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * - Neither the name of the copyright holder nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.eekboom.xlayouts;

import java.util.Map;
import java.util.HashMap;

/**
 * <code>ClassMap</code> can be configured with mappings where each mapping takes a Class instance and result in a
 * generic Value.
 * When queried with a class instance C it returns the value that maps from the closest ancestor of C, where closest
 * is defined as the nearest superclass or interface in a left-to right (superclass first, then interfaces),
 * breadth-first search of the ancestor tree.
 * A mapping from Object.class is only considered if no other mapping matches.
 * Null values are allowed. To distinguish a mapping to null from 'no applicable mapping' use the get() method that
 * takes a default value.
 */
class ClassMap {
    private Map class2Value = new HashMap();

    ClassMap() {
    }

    void put(Class clazz, Object value) {
        class2Value.put(clazz, value);
    }

    void remove(Class clazz) {
        class2Value.remove(clazz);
    }

    Object get(Class clazz) {
        return get(clazz, null);
    }

    Object get(Class clazz, Object defaultValue) {
        Result result = new Result();
        findValue(new Class[]{clazz}, 0, result);
        Object value;
        if(result.isSet()) {
            value = result.getValue();
        }
        else if(class2Value.containsKey(Object.class)) {
            value = class2Value.get(Object.class);
        }
        else {
            value = defaultValue;
        }
        return value;
    }

    private void findValue(Class[] classes, int currentDistance, Result result) {
        if(currentDistance >= result.distance) {
            return;
        }
        for(int i = 0; i < classes.length; i++) {
            Class clazz = classes[i];
            if(class2Value.containsKey(clazz)) {
                Object value = class2Value.get(clazz);
                result.set(value, currentDistance);
            }
            else {
                Class[] superClassifiers = getSuperClassifiers(clazz);
                findValue(superClassifiers, currentDistance + 1, result);
            }
        }
    }

    private static Class[] getSuperClassifiers(Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        Class superClass = clazz.getSuperclass();
        if(superClass == null || superClass == Object.class) {
            return interfaces;
        }
        Class[] superClassifiers = new Class[interfaces.length + 1];
        superClassifiers[0] = superClass;
        System.arraycopy(interfaces, 0, superClassifiers, 1, interfaces.length);
        return superClassifiers;
    }

    private static class Result {
        private Object value;
        private int distance = Integer.MAX_VALUE;
        private boolean isSet;

        private Result() {
        }

        private void set(Object value, int distance) {
            this.value = value;
            this.distance = distance;
            isSet = true;
        }

        private boolean isSet() {
            return isSet;
        }

        private Object getValue() {
            return value;
        }
    }
}
