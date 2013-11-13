package com.nhn.pinpoint.web.applicationmap;

import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.web.applicationmap.rawdata.Host;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.service.ComplexNodeId;
import com.nhn.pinpoint.web.service.Node;
import com.nhn.pinpoint.web.service.NodeId;
import com.nhn.pinpoint.web.service.SimpleNodeId;
import com.nhn.pinpoint.web.util.Mergeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * application map에서 application간의 관계를 담은 클래스
 * 
 * @author netspider
 */
public class ApplicationRelation implements Mergeable<NodeId, ApplicationRelation> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final NodeId id;

	protected final Application from;
	protected final Application to;
	private Map<String, Host> hostList;

//	public ApplicationRelation(Application from, Application to, Map<String, Host> hostList) {
//        if (from == null) {
//            throw new NullPointerException("from must not be null");
//        }
//        if (to == null) {
//            throw new NullPointerException("to must not be null");
//        }
//        ComplexNodeId fromId = from.getId();
//        ComplexNodeId toId = to.getId();
//        this.id = new ComplexNodeId(fromId.getSrc(), toId.getDest());
//        this.from = from;
//        this.to = to;
//        this.hostList = hostList;
//    }

    public ApplicationRelation(Application from, Application to, Map<String, Host> hostList) {
        if (from == null) {
            throw new NullPointerException("from must not be null");
        }
        if (to == null) {
            throw new NullPointerException("to must not be null");
        }
        SimpleNodeId fromId = (SimpleNodeId) from.getId();
        SimpleNodeId toId = (SimpleNodeId) to.getId();
        this.id = new ComplexNodeId(fromId.getKey(), toId.getKey());
        this.from = from;
        this.to = to;
        this.hostList = hostList;
    }

	public NodeId getId() {
		return id;
	}

	public Application getFrom() {
		return from;
	}

	public Application getTo() {
		return to;
	}

	public Map<String, Host> getHostList() {
		return hostList;
	}

	public void setHostList(Map<String, Host> hostList) {
		this.hostList = hostList;
	}

	public ResponseHistogram getHistogram() {
		ResponseHistogram result = null;

		for (Entry<String, Host> entry : hostList.entrySet()) {
			if (result == null) {
				// FIXME 뭔가 괴상한 방식이긴 하지만..
				ResponseHistogram histogram = entry.getValue().getHistogram();
				result = new ResponseHistogram(histogram.getId(), histogram.getServiceType());
			}
			result.mergeWith(entry.getValue().getHistogram());
		}
		return result;
	}

	@Override
	public ApplicationRelation mergeWith(ApplicationRelation relation) {
		// TODO this.equals로 바꿔도 되지 않을까?
		if (this.from.equals(relation.getFrom()) && this.to.equals(relation.getTo())) {
			// TODO Mergable value map을 만들어야 하나...
			for (Entry<String, Host> entry : relation.getHostList().entrySet()) {
				if (this.hostList.containsKey(entry.getKey())) {
					this.hostList.get(entry.getKey()).mergeWith(entry.getValue());
				} else {
					this.hostList.put(entry.getKey(), entry.getValue());
				}
			}
		} else {
            logger.info("from:{}, to:{}, relationFrom:{}, relationTo:{}", from, to, relation.getFrom(), relation.getTo());
			throw new IllegalArgumentException("Can't merge.");
		}
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplicationRelation other = (ApplicationRelation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ApplicationRelation [id=" + id + ", from=" + from + ", to=" + to + ", hostList=" + hostList + "]";
	}

}
