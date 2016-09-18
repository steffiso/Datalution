package datalog;

/**
 * This class stores kind and position values based on Magic Condition.
 */

public class PairForMagicCondition {
	private String kind;
	private int positionId;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public int getPositionId() {
		return positionId;
	}

	public void setPositionId(int positionId) {
		this.positionId = positionId;
	}

	public PairForMagicCondition(String kind, int positionId) {
		super();
		this.kind = kind;
		this.positionId = positionId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + positionId;
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
		PairForMagicCondition other = (PairForMagicCondition) obj;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		if (positionId != other.positionId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "kind=" + kind + ", positionId=" + positionId;
	}

}
