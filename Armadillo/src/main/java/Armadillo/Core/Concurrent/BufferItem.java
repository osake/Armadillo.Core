package Armadillo.Core.Concurrent;

import java.util.Comparator;
import java.util.Date;

public class BufferItem implements Comparable<BufferItem>,
		Comparator<BufferItem> {
	private Date m_date;
	private long m_lngAge;

	@Override
	public int compareTo(BufferItem arg0) {
		return compare(this, arg0);
	}

	@Override
	public int compare(BufferItem arg0, BufferItem arg1) {
		if (arg0.getAge() > arg1.getAge()) {
			return 1;
		}
		if (arg0.getAge() < arg1.getAge()) {
			return -1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		return (int) (m_lngAge ^ (m_lngAge >>> 32));
	}

	@Override
	public boolean equals(Object arg0) {
		return m_lngAge == ((BufferItem) arg0).m_lngAge;
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date m_date) {
		this.m_date = m_date;
	}

	public long getAge() {
		return m_lngAge;
	}

	public void setAge(long m_lngAge) {
		this.m_lngAge = m_lngAge;
	}
}
