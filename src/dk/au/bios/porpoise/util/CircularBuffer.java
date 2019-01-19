/*
 * Copyright (C) 2017-2019 Jacob Nabe-Nielsen <jnn@bios.au.dk>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 and only version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see 
 * <https://www.gnu.org/licenses>.
 * 
 * Linking DEPONS statically or dynamically with other modules is making a combined work based on DEPONS. 
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of DEPONS give you permission to combine DEPONS 
 * with free software programs or libraries that are released under the GNU LGPL and with code included in the 
 * standard release of Repast Simphony under the Repast Suite License (or modified versions of such code, with unchanged license). 
 * You may copy and distribute such a system following the terms of the GNU GPL for DEPONS and the licenses of the 
 * other code concerned.
 * 
 * Note that people who make modified versions of DEPONS are not obligated to grant this special exception for 
 * their modified versions; it is their choice whether to do so. 
 * The GNU General Public License gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */

package dk.au.bios.porpoise.util;

/**
 * A simple circular buffer (FIFO).
 *
 * @param <T> The type in the buffer.
 */
public class CircularBuffer<T> {

	private final Object[] buffer;
	private int idx;
	private int size; // number of elements

	public CircularBuffer(final int capacity) {
		buffer = new Object[capacity];
		idx = 0;
	}

	public void add(final T element) {
		buffer[idx] = element;
		idx = (idx + 1) % buffer.length;

		if (size < buffer.length) {
			size++;
		}
	}

	@SuppressWarnings("unchecked")
	public T get(final int i) {
		// element i is (idx-1)
		int elementIdx = (idx - 1) - i;

		if (elementIdx < 0) {
			elementIdx += buffer.length;
		}

		return (T) buffer[elementIdx];
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < size(); i++) {
			if (i != 0) {
				sb.append(" "); // match NetLogo formatting
			}

			sb.append(get(i));
		}
		sb.append("]");

		return sb.toString();
	}

}
