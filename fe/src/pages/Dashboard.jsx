import MainLayout from "../layouts/MainLayout";
import { Row, Col, Card, Statistic } from "antd";
import {
  MessageOutlined,
  ArrowRightOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

export default function Dashboard() {
  const navigate = useNavigate();

  return (
    <MainLayout>
      <h2 style={{ marginBottom: 20 }}>Dashboard Tổng quan</h2>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            <Card
              hoverable
              variant="borderless"
              onClick={() => navigate("/chat")}
              style={{ cursor: "pointer", background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)" }}
            >
              <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", color: "white" }}>
                <div>
                  <MessageOutlined style={{ fontSize: 24, marginBottom: 8, display: "block" }} />
                  <h3 style={{ color: "white", margin: 0 }}>Phòng Chat Nhóm</h3>
                  <span style={{ opacity: 0.8 }}>Thảo luận & Trao đổi</span>
                </div>
                <ArrowRightOutlined style={{ fontSize: 20 }} />
              </div>
            </Card>
          </div>
        </Col>
      </Row>
    </MainLayout>
  );
}
