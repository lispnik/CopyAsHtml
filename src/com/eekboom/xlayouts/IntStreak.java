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

public class IntStreak {
    private static final int INITIAL_CAPACITY = 4;

    private int[] data = new int[INITIAL_CAPACITY];
    private int   size;

    IntStreak() {
    }

    void clear() {
        size = 0;
        data = new int[INITIAL_CAPACITY];
    }

    int get(int index) {
        return data[index];
    }

    void set(int index, int value) {
        ensure(index);
        data[index] = value;
    }

    void maximize(int index, int value) {
        ensure(index);
        data[index] = Math.max(data[index], value);
    }

    private void ensure(int index) {
        if(index >= data.length) {
            int[] newData = new int[(int)((index + 4) * 1.2)];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
        size = Math.max(size, index + 1);
    }
}
