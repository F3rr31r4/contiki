/*
 * Copyright (c) 2009, Swedish Institute of Computer Science
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 * $Id: CoffeeFile.java,v 1.1 2009/08/04 10:36:53 nvt-se Exp $
 *
 * @author Nicolas Tsiftes
 *
 */

package se.sics.coffee;

import java.io.*;

public class CoffeeFile {
	private CoffeeFS coffeeFS;
	private CoffeeHeader header;
	private String name;
	private int length;
	private int startPage;
	private int reservedSize;
	private CoffeeFile logFile;
	private boolean knownLength;

	public CoffeeFile(CoffeeFS coffeeFS, CoffeeHeader header) {
		this.coffeeFS = coffeeFS;
		this.header = header;
		name = header.name;
		startPage = header.getPage();
		reservedSize = header.maxPages * coffeeFS.getConfiguration().pageSize;
	}

	private int calculateLength() throws IOException {
		byte[] bytes = new byte[1];
		int i;

		for(i = reservedSize; i >= header.rawLength(); i--) {
			coffeeFS.getImage().read(bytes, 1, header.getPage() * coffeeFS.getConfiguration().pageSize + i);
			if(bytes[0] != 0) {
				return i - header.rawLength() + 1;
			}
		}
		return 0;
	}

	public void insertContents(FileInputStream input) throws IOException {
		byte[] bytes = new byte[1];
		int ch;
		int startOffset = header.getPage() *
				  coffeeFS.getConfiguration().pageSize +
				  header.rawLength();

		length = 0;
		knownLength = true;

		while((ch = input.read()) != -1) {
			bytes[0] = (byte) ch;

			coffeeFS.getImage().write(bytes, 1,
				startOffset + length);
			length++;
		}
System.out.println("inserted bytes: " + length);
	}

	public void saveContents(String filename) throws IOException {
		byte[] bytes = new byte[1];
		int startOffset = header.getPage() *
				  coffeeFS.getConfiguration().pageSize +
				  header.rawLength();
		int i;

		FileOutputStream fOut = new FileOutputStream(filename);
		for(i = 0; i < getLength(); i++) {
			coffeeFS.getImage().read(bytes, 1, startOffset + i);
			fOut.write(bytes);
		}

		fOut.close();
	}

	public void remove() {
		header.makeObsolete();
	}

	public CoffeeHeader getHeader() {
		return header;
	}

	public int getLength() throws IOException {
		if(!knownLength) {
			length = calculateLength();
			knownLength = true;
		}
		return length;
	}

	public String getName() {
		return name;
	}

	public int getReservedSize() {
		return reservedSize;
	}
}