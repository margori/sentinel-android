package com.samourai.sentinel.util;

import android.util.Patterns;

import com.samourai.sentinel.SamouraiSentinel;
import com.samourai.sentinel.segwit.bech32.Bech32;
import com.samourai.sentinel.segwit.bech32.Bech32Segwit;

import org.apache.commons.lang3.tuple.Pair;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.WrongNetworkException;
import org.bitcoinj.uri.BitcoinURI;
import org.bitcoinj.uri.BitcoinURIParseException;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import android.util.Log;

public class FormatsUtil {

	private Pattern emailPattern = Patterns.EMAIL_ADDRESS;
	private Pattern phonePattern = Pattern.compile("(\\+[1-9]{1}[0-9]{1,2}+|00[1-9]{1}[0-9]{1,2}+)[\\(\\)\\.\\-\\s\\d]{6,16}");

	private String URI_BECH32 = "(^bitcoin:(tb|bc)1([qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)(\\?amount\\=([0-9.]+))?$)|(^bitcoin:(TB|BC)1([QPZRY9X8GF2TVDW0S3JN54KHCE6MUA7L]+)(\\?amount\\=([0-9.]+))?$)";
	private String URI_BECH32_LOWER = "^bitcoin:((tb|TB|bc|BC)1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)(\\?amount\\=([0-9.]+))?$";

	public static final int MAGIC_XPUB = 0x0488B21E;
	public static final int MAGIC_TPUB = 0x043587CF;
	public static final int MAGIC_YPUB = 0x049D7CB2;
	public static final int MAGIC_UPUB = 0x044A5262;
	public static final int MAGIC_ZPUB = 0x04B24746;
	public static final int MAGIC_VPUB = 0x045F1CF6;

	public static final String XPUB = "^[xtyu]pub[1-9A-Za-z][^OIl]+$";
	public static final String HEX = "^[0-9A-Fa-f]+$";

	private static FormatsUtil instance = null;

	private FormatsUtil() { ; }

	public static FormatsUtil getInstance() {

		if(instance == null) {
			instance = new FormatsUtil();
		}

		return instance;
	}

	public String validateBitcoinAddress(final String address) {

		if(isValidBitcoinAddress(address)) {
			return address;
		}
		else {
			String addr = getBitcoinAddress(address);
			if(addr != null) {
				return addr;
			}
			else {
				return null;
			}
		}
	}

	public boolean isBitcoinUri(final String s) {

		boolean ret = false;
		BitcoinURI uri = null;

		try {
			uri = new BitcoinURI(s);
			ret = true;
		}
		catch(BitcoinURIParseException bupe) {
			if(s.matches(URI_BECH32))	{
				ret = true;
			}
			else	{
				ret = false;
			}
		}

		return ret;
	}

	public String getBitcoinUri(final String s) {

		String ret = null;
		BitcoinURI uri = null;

		try {
			uri = new BitcoinURI(s);
			ret = uri.toString();
		}
		catch(BitcoinURIParseException bupe) {
			if(s.matches(URI_BECH32))	{
				return s;
			}
			else	{
				ret = null;
			}
		}

		return ret;
	}

	public String getBitcoinAddress(final String s) {

		String ret = null;
		BitcoinURI uri = null;

		try {
			uri = new BitcoinURI(s);
			ret = uri.getAddress().toString();
		}
		catch(BitcoinURIParseException bupe) {
			if(s.toLowerCase().matches(URI_BECH32_LOWER))	{
				Pattern pattern = Pattern.compile(URI_BECH32_LOWER);
				Matcher matcher = pattern.matcher(s.toLowerCase());
				if(matcher.find() && matcher.group(1) != null)    {
					return matcher.group(1);
				}
			}
			else	{
				ret = null;
			}
		}

		return ret;
	}

	public String getBitcoinAmount(final String s) {

		String ret = null;
		BitcoinURI uri = null;

		try {
			uri = new BitcoinURI(s);
			if(uri.getAmount() != null) {
				ret = uri.getAmount().toString();
			}
			else {
				ret = "0.0000";
			}
		}
		catch(BitcoinURIParseException bupe) {
			if(s.toLowerCase().matches(URI_BECH32_LOWER))	{
				Pattern pattern = Pattern.compile(URI_BECH32_LOWER);
				Matcher matcher = pattern.matcher(s.toLowerCase());
				if(matcher.find() && matcher.group(4) != null)    {
					String amt = matcher.group(4);
					try	{
						return Long.toString(Math.round(Double.valueOf(amt) * 1e8));
					}
					catch(NumberFormatException nfe)	{
						ret = "0.0000";
					}
				}
			}
			else	{
				ret = null;
			}
		}

		return ret;
	}

	public boolean isValidBitcoinAddress(final String address) {

		boolean ret = false;
		Address addr = null;

		if(address.toLowerCase().startsWith("bc") || address.toLowerCase().startsWith("tb"))	{

			try	{
				Pair<Byte, byte[]> pair = Bech32Segwit.decode(address.substring(0, 2), address);
				if(pair.getLeft() == null || pair.getRight() == null)	{
					;
				}
				else	{
					ret = true;
				}
			}
			catch(Exception e)	{
				e.printStackTrace();
			}

		}
		else	{

			try {
				addr = new Address(SamouraiSentinel.getInstance().getCurrentNetworkParams(), address);
				if(addr != null) {
					ret = true;
				}
			}
			catch(WrongNetworkException wne) {
				ret = false;
			}
			catch(AddressFormatException afe) {
				ret = false;
			}

		}

		return ret;
	}

	public boolean isValidBech32(final String address) {

		boolean ret = false;

		try	{
			Pair<String, byte[]> pair0 = Bech32.bech32Decode(address);
			if(pair0.getLeft() == null || pair0.getRight() == null)	{
				ret = false;
			}
			else	{
				Pair<Byte, byte[]> pair1 = Bech32Segwit.decode(address.substring(0, 2), address);
				if(pair1.getLeft() == null || pair1.getRight() == null)	{
					ret = false;
				}
				else	{
					ret = true;
				}
			}
		}
		catch(Exception e)	{
			ret = false;
		}

		return ret;
	}

	public boolean isValidXpub(String xpub){

		try {
			byte[] xpubBytes = Base58.decodeChecked(xpub);

			ByteBuffer byteBuffer = ByteBuffer.wrap(xpubBytes);
			int version = byteBuffer.getInt();
			if(version != MAGIC_XPUB && version != MAGIC_TPUB && version != MAGIC_YPUB && version != MAGIC_UPUB && version != MAGIC_ZPUB && version != MAGIC_VPUB)   {
				throw new AddressFormatException("invalid version: " + xpub);
			}
			else	{

				byte[] chain = new byte[32];
				byte[] pub = new byte[33];
				// depth:
				byteBuffer.get();
				// parent fingerprint:
				byteBuffer.getInt();
				// child no.
				byteBuffer.getInt();
				byteBuffer.get(chain);
				byteBuffer.get(pub);

				ByteBuffer pubBytes = ByteBuffer.wrap(pub);
				int firstByte = pubBytes.get();
				if(firstByte == 0x02 || firstByte == 0x03){
					return true;
				}else{
					return false;
				}
			}
		}
		catch(Exception e)	{
			return false;
		}
	}

}
