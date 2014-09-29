package Armadillo.Core.Concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Math.BinarySearch;

public class EfficientMemoryBuffer<TK, TV> {
	private double m_dblBufferLifeMins;
	private int m_intCapacity;
	private HashMap<TK, TV> m_data;
	private HashMap<BufferItem, TK> m_ageMap;
	private HashMap<TK, BufferItem> m_keyToAge;
	private ArrayList<BufferItem> m_ages;
	private long m_lngAge;
	private Object m_lockObj = new Object();
	private int m_intWaitMills;

	public EfficientMemoryBuffer(int intCapacity) 
	{
		this(intCapacity, 0);
	}

	public EfficientMemoryBuffer(int intCapacity, double dblBufferLifeMins) 
	{
		m_dblBufferLifeMins = dblBufferLifeMins;
		m_intCapacity = intCapacity;
		m_data = new HashMap<TK, TV>();
		m_ages = new ArrayList<BufferItem>();
		m_ageMap = new HashMap<BufferItem, TK>();
		m_keyToAge = new HashMap<TK, BufferItem>();

		if (dblBufferLifeMins > 0) {
			m_intWaitMills = (int) Math.min(60000, dblBufferLifeMins * 60000);
			ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>() {
				@Override
				public void runTask(ObjectWrapper item) {
					while (true) {
						try {
							Thread.sleep(m_intWaitMills);
							ArrayList<Map.Entry<TK, BufferItem>> mapSet;
							synchronized (m_lockObj) {
								mapSet = new ArrayList<Map.Entry<TK, BufferItem>>(
										m_keyToAge.entrySet());
							}
							DateTime now = DateTime.now();

							for (int i = 0; i < mapSet.size(); i++) {
								Map.Entry<TK, BufferItem> currEntry = mapSet
										.get(i);
								Minutes period = Minutes.minutesBetween(new DateTime(
										currEntry.getValue().getDate()), now);

								double dblMinutes = period.getMinutes();
								if (dblMinutes > m_dblBufferLifeMins) {
									remove(currEntry.getKey());
								}
							}
						} catch (Exception ex) {
							Logger.log(ex);
						}
					}
				}
			};
			worker.work();
		}
	}

	public boolean containsKey(TK key) {
		synchronized (m_lockObj) {
			return m_data.containsKey(key);
		}
	}

	public void put(TK key, TV value) throws Exception {
		
		TV oldValue = null;
		synchronized (m_lockObj) {
			//
			// used for safety, avoid adding the same key twice
			// otherwise this will cause problems with the age list and the age
			// map
			//
			removeUnsafe(key);

			m_lngAge++;
			m_data.put(key, value);
			BufferItem ageItem = getAgeItem();
			m_ageMap.put(ageItem, key);
			m_keyToAge.put(key, ageItem);
			m_ages.add(ageItem);

			//
			// check capacity
			//
			if (m_data.size() > m_intCapacity) {
				BufferItem ageToRemove = m_ages.get(0);
				m_ages.remove(0);

				TK keyToRemove = m_ageMap.get(ageToRemove);

				if (!m_ageMap.containsKey(ageToRemove)) {
					throw new Exception("Item not found");
				}
				m_ageMap.remove(ageToRemove);

				if (!m_keyToAge.containsKey(keyToRemove)) {
					throw new Exception("Item not found");
				}
				m_keyToAge.remove(keyToRemove);

				oldValue = m_data.get(keyToRemove);

				if (!m_data.containsKey(keyToRemove)) {
					throw new Exception("Item not found");
				}
				m_data.remove(keyToRemove);

			}
		}
		if(oldValue != null){
			onItemRemoved(oldValue);
		}
	}

	public void onItemRemoved(TV oldValue) {
	}

	private BufferItem getAgeItem() {
		BufferItem bufferItem = new BufferItem();
		bufferItem.setAge(m_lngAge);
		bufferItem.setDate(DateTime.now().toDate());
		return bufferItem;
	}

	public boolean Remove(TK key) {
		synchronized (m_lockObj) {
			return removeUnsafe(key);
		}
	}

	private boolean removeUnsafe(TK key) {
		boolean blnRemove = false;
		if (m_keyToAge.containsKey(key)) {
			BufferItem lngAge = m_keyToAge.get(key);
			blnRemove = true;
			m_ageMap.remove(lngAge);
			int intIndex = BinarySearch.binarySearch(m_ages, lngAge);
			m_ages.remove(intIndex);
			m_data.remove(key);
			m_keyToAge.remove(key);
		}
		return blnRemove;
	}

	public TV get(TK key) throws Exception {
		synchronized (m_lockObj) {
			TV value = null;
			if (m_data.containsKey(key)) {
				value = m_data.get(key);
				m_lngAge++;
				BufferItem ageItem = getAgeItem();
				BufferItem oldAge = m_keyToAge.get(key);
				m_keyToAge.remove(key);
				m_keyToAge.put(key, ageItem);

				if (!m_ageMap.containsKey(oldAge)) {
					throw new Exception("Age not found");
				}
				m_ageMap.put(ageItem, key);
				m_ageMap.remove(oldAge);
				int intIndex = BinarySearch.binarySearch(m_ages, oldAge);
				m_ages.remove(intIndex);
				m_ages.add(ageItem);
			}
			return value;
		}
	}

	public void clear() {
		synchronized (m_lockObj) {
			m_lngAge = 0;
			m_ageMap.clear();
			m_ages.clear();
			m_data.clear();
			m_keyToAge.clear();
		}
	}

	private boolean remove(TK key) {
		synchronized (m_lockObj) {
			return removeUnsafe(key);
		}
	}

	public int count() {
		synchronized (m_lockObj) {
			return m_data.size();
		}
	}
}
