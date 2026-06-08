package io.avaje.metrics.ebean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EbeanMetricNamingTest {

  @Test
  void iud_mapsToDml() {
    var id = EbeanMetricNaming.toId("iud.BProcessLog.insertBatch");
    assertThat(id.name()).isEqualTo("ebean.dml");
    assertThat(id.tags().array()).containsExactly("label:BProcessLog.insertBatch");
  }

  @Test
  void dto_mapsToQueryWithKind() {
    var id = EbeanMetricNaming.toId("dto.SensorState");
    assertThat(id.name()).isEqualTo("ebean.query");
    assertThat(id.tags().array()).containsExactly("kind:dto", "label:SensorState");
  }

  @Test
  void orm_mapsToQueryWithKind() {
    var id = EbeanMetricNaming.toId("orm.Organisation.findById");
    assertThat(id.name()).isEqualTo("ebean.query");
    assertThat(id.tags().array()).containsExactly("kind:orm", "label:Organisation.findById");
  }

  @Test
  void sql_mapsToQueryWithKind() {
    var id = EbeanMetricNaming.toId("sql.someRawQuery");
    assertThat(id.name()).isEqualTo("ebean.query");
    assertThat(id.tags().array()).containsExactly("kind:sql", "label:someRawQuery");
  }

  @Test
  void orm_withBeanType_addsTypeTag() {
    var id = EbeanMetricNaming.toId("orm.Customer.custMain.contacts.lazy", "Contact");
    assertThat(id.name()).isEqualTo("ebean.query");
    assertThat(id.tags().array())
      .containsExactly("kind:orm", "type:Contact", "label:Customer.custMain.contacts.lazy");
  }

  @Test
  void dto_withBeanType_addsTypeTag() {
    var id = EbeanMetricNaming.toId("dto.SensorState", "SensorState");
    assertThat(id.name()).isEqualTo("ebean.query");
    assertThat(id.tags().array()).containsExactly("kind:dto", "type:SensorState", "label:SensorState");
  }

  @Test
  void query_nullBeanType_omitsTypeTag() {
    var id = EbeanMetricNaming.toId("orm.Organisation.findById", null);
    assertThat(id.tags().array()).containsExactly("kind:orm", "label:Organisation.findById");
  }

  @Test
  void query_emptyBeanType_omitsTypeTag() {
    var id = EbeanMetricNaming.toId("orm.Organisation.findById", "");
    assertThat(id.tags().array()).containsExactly("kind:orm", "label:Organisation.findById");
  }

  @Test
  void txnNamed_mapsToTxn_stripsNamedPrefix() {
    var id = EbeanMetricNaming.toId("txn.named.MapDailyMachine.run");
    assertThat(id.name()).isEqualTo("ebean.txn");
    assertThat(id.tags().array()).containsExactly("label:MapDailyMachine.run");
  }

  @Test
  void txnBare_mapsToTxn() {
    var id = EbeanMetricNaming.toId("txn.SomeTransaction");
    assertThat(id.name()).isEqualTo("ebean.txn");
    assertThat(id.tags().array()).containsExactly("label:SomeTransaction");
  }

  @Test
  void l2_withRegionAndOp() {
    var id = EbeanMetricNaming.toId("l2.beanCache.hit");
    assertThat(id.name()).isEqualTo("ebean.l2");
    assertThat(id.tags().array()).containsExactly("op:hit", "region:beanCache");
  }

  @Test
  void l2_opOnly() {
    var id = EbeanMetricNaming.toId("l2.miss");
    assertThat(id.name()).isEqualTo("ebean.l2");
    assertThat(id.tags().array()).containsExactly("op:miss");
  }

  @Test
  void unrecognised_mapsToOther_withFullLabel() {
    var id = EbeanMetricNaming.toId("custom.thing");
    assertThat(id.name()).isEqualTo("ebean.other");
    assertThat(id.tags().array()).containsExactly("label:custom.thing");
  }

  @Test
  void noPrefix_mapsToOther() {
    var id = EbeanMetricNaming.toId("plain");
    assertThat(id.name()).isEqualTo("ebean.other");
    assertThat(id.tags().array()).containsExactly("label:plain");
  }

  @Test
  void empty_mapsToOther() {
    var id = EbeanMetricNaming.toId("");
    assertThat(id.name()).isEqualTo("ebean.other");
    assertThat(id.tags().isEmpty()).isTrue();
  }

  @Test
  void nullName_mapsToOther() {
    var id = EbeanMetricNaming.toId(null);
    assertThat(id.name()).isEqualTo("ebean.other");
    assertThat(id.tags().isEmpty()).isTrue();
  }
}
