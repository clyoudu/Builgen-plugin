package builgen.bean;

public class TestBean {

	private String sd;
	private String[] sdf;
	private int we;
	private int[] sdfsdf;

	public TestBean(TestBean testBean) {
		this.sd = testBean.getSd();
		this.sdf = testBean.getSdf();
		this.we = testBean.getWe();
		this.sdfsdf = testBean.getSdfsdf();
	}

	public TestBean() {
	}

	public static class TestBeanBuilder {
		TestBean testBean;

		public TestBeanBuilder() {
			testBean = new TestBean();
		}

		public TestBeanBuilder sd(String sd) {
			testBean.setSd(sd);
			return this;
		}

		public TestBeanBuilder sdf(String[] sdf) {
			testBean.setSdf(sdf);
			return this;
		}

		public TestBeanBuilder we(int we) {
			testBean.setWe(we);
			return this;
		}

		public TestBeanBuilder sdfsdf(int[] sdfsdf) {
			testBean.setSdfsdf(sdfsdf);
			return this;
		}

		public TestBean build() {
			return new TestBean(this.testBean);
		}
	}

	public void setSdfsdf(int[] sdfsdf) {
		this.sdfsdf = sdfsdf;
	}

	public int[] getSdfsdf() {
		return this.sdfsdf;
	}

	public void setWe(int we) {
		this.we = we;
	}

	public int getWe() {
		return this.we;
	}

	public void setSdf(String[] sdf) {
		this.sdf = sdf;
	}

	public String[] getSdf() {
		return this.sdf;
	}

	public void setSd(String sd) {
		this.sd = sd;
	}

	public String getSd() {
		return this.sd;
	}

}
