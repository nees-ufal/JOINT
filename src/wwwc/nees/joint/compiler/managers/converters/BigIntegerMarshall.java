/*
 * Copyright (c) 2007-2009, James Leigh All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution. 
 * - Neither the name of the openrdf.org nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package wwwc.nees.joint.compiler.managers.converters;

import java.math.BigInteger;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import wwwc.nees.joint.compiler.managers.Marshall;

/**
 * Converts BigInteger to and from Literal.
 * 
 * @author James Leigh
 *
 */
public class BigIntegerMarshall implements Marshall<BigInteger> {
	private ValueFactory vf;

	private URI datatype;

	public BigIntegerMarshall(ValueFactory vf) {
		this.vf = vf;
		datatype = XMLSchema.INTEGER;
	}

	public String getJavaClassName() {
		return BigInteger.class.getName();
	}

	public URI getDatatype() {
		return datatype;
	}

	public void setDatatype(URI dt) {
		if (!dt.equals(datatype))
			throw new IllegalArgumentException(dt.toString());
	}

	public BigInteger deserialize(Literal literal) {
		return literal.integerValue();
	}

	public Literal serialize(BigInteger object) {
		// TODO add ValueFactory#createLiteral(BigInteger) method
		return vf.createLiteral(object.toString(), datatype);
	}

}
