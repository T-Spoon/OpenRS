/**
* Copyright (c) Kyle Fricilone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.openrs.cache.type;

/**
 * @author Kyle Friz
 *
 * @since May 27, 2015
 */
public enum ConfigArchive {

	AREA(35),
	ENUM(8),
	HITBAR(33),
	HITMARK(32),
	IDENTKIT(3),
	ITEM(10),
	INV(5),
	NPC(9),
	OBJECT(6),
	OVERLAY(4),
	PARAMS(11),
	SEQUENCE(12),
	SPOTANIM(13),
	STRUCT(34),
	UNDERLAY(1),
	VARBIT(14),
	VARCLIENT(19),
	VARCLIENTSTRING(15),
	VARPLAYER(16);

	private final int id;

	ConfigArchive(int id) {
		this.id = id;
	}

	public final int getID() {
		return id;
	}

}
